import { DropDownList } from "@progress/kendo-react-dropdowns";
import React, { useState } from "react";
import {
  Upload,
  UploadFileInfo,
  UploadOnAddEvent,
  UploadOnRemoveEvent,
} from "@progress/kendo-react-upload";
import { Field, Form } from "@progress/kendo-react-form";
import { Checkbox } from "@progress/kendo-react-inputs";
import "./RUpload.scss";

interface FieldRenderPropsDropdown {
  value: string;
  onChange: (value: { value: string }) => void;
}
interface EventDropdown {
  target: {
    value: {
      id: string;
    };
  };
}
const DropDownListInput = (
  fieldRenderPropsDropdown: FieldRenderPropsDropdown
) => {
  const { value, onChange, ...others } = fieldRenderPropsDropdown;
  const data = [
    {
      id: "getId",
      name: "Get ID",
    },
    {
      id: "getDocument",
      name: "Get document",
    },
  ];
  const handleChange = (event: EventDropdown) => {
    const outputValue = event.target.value.id === "getDocument" ? "docx" : "id";
    onChange({ value: outputValue });
  };
  return (
    <DropDownList
      data={data}
      textField="name"
      dataItemKey="id"
      value={data.find((item) => item.id === value)}
      onChange={handleChange}
      {...others}
    />
  );
};

interface FieldRenderProps {
  value: UploadFileInfo[];
  onChange: (value: { value: UploadFileInfo[] }) => void;
}

const UploadInput = (fieldRenderProps: FieldRenderProps) => {
  const onChangeHandler = (event: UploadOnAddEvent) => {
    fieldRenderProps.onChange({ value: event.newState });
  };
  const onRemoveHandler = (event: UploadOnRemoveEvent) => {
    fieldRenderProps.onChange({ value: event.newState });
  };
  return (
    <Upload
      autoUpload={false}
      showActionButtons={false}
      files={fieldRenderProps.value}
      onAdd={onChangeHandler}
      onRemove={onRemoveHandler}
    />
  );
};

type FileWithRaw = {
  getRawFile: () => File;
};
const RUpload = () => {
  const [s3Key, setS3Key] = useState<string | null>(null);

  const handleSubmit = async (dataItem: {
    // eslint-disable-next-line
    [name: string]: any;
  }) => {
    const { files, ...otherFields } = dataItem;

    const formData = new FormData();

    files.forEach((file: FileWithRaw) => {
      formData.append("files", file.getRawFile());
    });

    Object.keys(otherFields).forEach((prop) => {
      formData.append(prop, otherFields[prop]);
    });

    try {
      const response = await fetch("http://localhost:8080/api/extractors", {
        method: "POST",
        body: formData,
      });

      if (dataItem.output === "docx") {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = files[0].getRawFile().name.replace(".R", ".docx");
        document.body.appendChild(a);
        a.click();
        a.remove();
      } else {
        const data = await response.json();
        setS3Key(data["s3 key"]);
      }
    } catch (error) {
      console.error("Error:", error);
    }
  };

  return (
    <Form
      onSubmit={handleSubmit}
      render={(formRenderProps) => (
        <form onSubmit={formRenderProps.onSubmit} className={"k-form"}>
          <fieldset>
            <legend>Upload your R files:</legend>
            <div className="upload-input">
              <Field name={"files"} component={UploadInput} />
            </div>
            <div className="dropdown-list">
              <Field
                name={"output"}
                component={DropDownListInput}
                label={"Select your returning parameter"}
              />
            </div>
            <div className="checkbox">
              <Field
                name={"pdf"}
                component={Checkbox}
                label={
                  "Do you want whole plot movement instead of one picture?"
                }
              />
            </div>
            <div className="s3">
              Your S3 key for download:{" "}
              {s3Key && <input type="text" value={` ${s3Key}`} readOnly />}
            </div>
            <div className="submit-button">
              <button
                type={"submit"}
                className="k-button"
                disabled={!formRenderProps.allowSubmit}
              >
                Submit
              </button>
            </div>
          </fieldset>
        </form>
      )}
    />
  );
};

export default RUpload;
